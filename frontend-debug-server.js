//
// frontend-debug-server.js
// RegEx Frontend
//
// Created on 2/26/17
//

const { resolve } = require('path')
const express     = require('express')

function staticFile (app, endpoint, relative) {
  app.get(endpoint, (req, res) => {
    res.sendFile(resolve(__dirname, relative))
  })
}

function staticDir (app, endpoint, relative) {
  app.use(endpoint, express.static(resolve(__dirname, relative)))
}

const port = process.env.PORT || 8080
const app = express()

staticFile(app, '/', './src/main/resources/spark/template/freemarker/frontend.html')
staticDir(app, '/', './src/main/resources/dist')

app.listen(port)
console.log(`http://localhost:${port}`)
