// Set custom slackId to the payload

env.payload = {
   "userId": "9efbc1bf-0145-4f19-aa04-2d1337cb59da",
   "message": "A new task " + env.taskId + " was created in the project " + env.projectId + " and assigned to a user " + env.slackId
}