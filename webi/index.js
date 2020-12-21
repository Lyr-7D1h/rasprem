const fastify = require("fastify")();
const { exec } = require("child_process");
const path = require("path");

fastify.register(require("fastify-static"), {
  root: path.join(__dirname, "public"),
});

fastify.post("/credentials", (request, reply) => {
  if (!request.body.SSID) {
    reply.status(401);
    return reply.send({ message: "SSID not found" });
  }
  if (!request.body.pass) {
    reply.status(401);
    return reply.send({ message: "pass not found" });
  }

  // TODO: Check connection given
  exec(``);

  reply.send({ message: "Success" });
});

// TODO: Low power mode

fastify.listen(5000, function (err, address) {
  if (err) {
    console.error(err);
    process.exit(1);
  }
  console.log(`server listening on ${address}`);
});
