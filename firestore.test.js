const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProgressPayload(uid) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    displayName: "Alice Dev",
    currentDay: 1,
    highestCompletedDay: 0,
    streakDays: 0,
    totalXp: 0,
    correctAnswers: 0,
    totalAnswers: 0,
    nextUnlockEpochMs: 0,
    yesterdayQuizPassedForDay: 1,
    createdAt: now,
    updatedAt: now,
  };
}

function validLessonPayload(dayNumber) {
  const now = new Date(Date.now() - 1000);
  return {
    dayNumber,
    topic: "HTTP, TLS и архитектура LLM API",
    category: "NETWORKING",
    summary: "Основы сетевого взаимодействия с ИИ-моделями.",
    content: "Подробный разбор HTTP/2, TLS и потоковой передачи SSE.",
    question: "Какой протокол используется для потоковой передачи токенов в веб-API?",
    options: ["SSE (Server-Sent Events)", "FTP", "SMTP", "ICMP"],
    correctAnswerIndex: 0,
    explanation: "SSE позволяет серверу отправлять токены в реальном времени поверх HTTP.",
    deepDiveContent: "",
    weeklyAssessment: false,
    weeklyQuestions: [],
    xpReward: 100,
    estimatedMinutes: 5,
    completed: false,
    quizScore: 0,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated users cannot read or write progress or lessons", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProgressPayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("lessons").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("lessons").doc("day_1").set(validLessonPayload(1))
  );
});

test("Authenticated owner can create, read, and partially update own progress and lessons", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const progressRef = aliceDb.collection("users").doc(ALICE_UID);
  const lessonsCol = progressRef.collection("lessons");

  await assertSucceeds(progressRef.set(validProgressPayload(ALICE_UID)));
  await assertSucceeds(progressRef.get());

  await assertSucceeds(
    progressRef.update({
      currentDay: 2,
      highestCompletedDay: 1,
      streakDays: 1,
      totalXp: 100,
      nextUnlockEpochMs: Date.now() + 86400000,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(lessonsCol.doc("day_1").set(validLessonPayload(1)));
  await assertSucceeds(lessonsCol.get());
  await assertSucceeds(
    lessonsCol.doc("day_1").update({
      deepDiveContent: "## Расширенный технический разбор",
      completed: true,
      quizScore: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user reads and writes are strictly rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set(validProgressPayload(BOB_UID));
    await db.collection("users").doc(BOB_UID).collection("lessons").doc("day_1").set(validLessonPayload(1));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).update({ totalXp: 999 }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("lessons").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("lessons").doc("day_1").update({ completed: true })
  );
});

test("Shadow fields, @DocumentId 'id', immutable field tampering, and invalid timestamps fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const progressRef = aliceDb.collection("users").doc(ALICE_UID);

  // Ghost field rejected
  await assertFails(
    progressRef.set({
      ...validProgressPayload(ALICE_UID),
      isAdmin: true,
    })
  );

  // @DocumentId 'id' in payload rejected
  await assertFails(
    progressRef.collection("lessons").doc("day_1").set({
      ...validLessonPayload(1),
      id: "day_1",
    })
  );

  // Future timestamp rejected
  await assertFails(
    progressRef.set({
      ...validProgressPayload(ALICE_UID),
      createdAt: new Date(Date.now() + 60000),
    })
  );

  // Create valid, then reject immutable createdAt mutation
  await assertSucceeds(progressRef.set(validProgressPayload(ALICE_UID)));
  await assertFails(
    progressRef.update({
      createdAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 200),
    })
  );
});
