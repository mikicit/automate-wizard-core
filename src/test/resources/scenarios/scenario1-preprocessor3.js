// Set the payload to the slackId from the environment

env.payload = {
    "userId": env.slackId,
    "message": "You have been assigned a new task " + env.taskId + " in Jira project " + env.projectId
}