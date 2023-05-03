#!/usr/bin/env node
const { exec } = require("node:child_process");
const { readFileSync } = require("node:fs");
const http = require("node:http");

const html = readFileSync("./index.html");

const server = http.createServer();

function requestHandler(req, res, data) {
  switch (req.url) {
    case "/credentials":
      const { ssid, pass } = data;
      console.log(ssid, pass);
      exec(`iwconfig wlan0 essid ${ssid} key ${pass}`);
      res.write("ok");
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

server.listen(5000);
