// Get a random participant from the list of participants or stop the scenario if there are no participants

if (env.payload.participants.length !== 0) {
    var randomIndex = Math.floor(Math.random() * env.payload.participants.length);
    var randomParticipant = env.payload.participants[randomIndex];

    env.slackId = randomParticipant.slackId;
    env.payload = {
        projectId: env.projectId,
        taskId: env.taskId,
        userId: randomParticipant.userId
    };
}