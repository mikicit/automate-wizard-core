// Save the project ID and task ID to the environment and set the payload to the project ID

env.projectId = env.payload.projectId;
env.taskId = env.payload.taskId;
env.payload = {
    "projectId": env.projectId
}