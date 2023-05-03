#!/usr/bin/env node
const { readFileSync } = require("node:fs");
const http = require("node:http");

const html = readFileSync("./index.html");

const server = http.createServer();

server.on("request", (req, res) => {
  let data = "";

  req.on("data", (b) => {
    data += b;
  });

  req.on("error", console.error);

  req.on("end", () => {
    const url = new URL(req.url);
    switch (url.pathname) {
      case "credentials":
        return "ok";
      default:
        res.setHeader("content-type", "text/html; charset=utf-8");
        return html;
    }
  });
});

server.on("clientError", (err, socket) => {
  socket.end("HTTP/1.1 400 Bad Request\r\n\r\n");
});

server.listen(5000);
