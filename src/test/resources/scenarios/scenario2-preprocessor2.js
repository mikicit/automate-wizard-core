// Check price value and pass execution with payload to SMS sender

if (env.payload.html.body < 30) {
    env.payload = {
        phoneNumber: '1234567890',
        message: 'Price is low, buy now!'
    };
} else {
    env.process = false;
}