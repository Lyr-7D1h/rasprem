#!/usr/bin/env node
const util = require("util");
const http = require("http");
const { readFileSync } = require("fs");
const { join } = require("path");
const exec = util.promisify(require("child_process").exec);

const PORT = process.env.PORT || 5000;
const html = readFileSync(join(__dirname, "index.html"));

const server = http.createServer();

async function requestHandler(req, res, data) {
  switch (req.url) {
    case "/credentials":
      const { ssid, pass } = JSON.parse(data);
      await exec(`iwconfig wlan0 essid "${ssid}" key "${pass}"`);
      res.write("ok");
      break;
    case "/networks":
      const { stdout } = await exec("iwlist wlan0 scan");
      console.log(stdout);
      break;
    default:
      res.setHeader("content-type", "text/html; charset=utf-8");
      res.write(html);
  }

  res.end();
}

server.on("request", (req, res) => {
  let data = "";

  req.on("data", (b) => {
    data += b;
  });

  req.on("error", console.error);

  req.on("end", () => requestHandler(req, res, data));
});

server.on("error", console.error);

server.on("clientError", (err, socket) => {
  socket.end("HTTP/1.1 400 Bad Request\r\n\r\n");
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Listening on port ${PORT}...`);
});
