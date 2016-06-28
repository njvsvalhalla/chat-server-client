import net from 'net'
import vorpal from 'vorpal'

const cli = vorpal()

// cli config
cli
  .delimiter('ftd-chat~$')

// connect mode
let server
let username

cli
  .mode('connect [host] [username] <port>')
  .delimiter('connected:')
  .init(function (args, callback) {
    username = args.username
    server = net.createConnection(args, () => {
      const address = server.address()
      this.log(`connected to server ${username} @ ${address.address}:${address.port}`)
      callback()
    })

    if (username) {
      server.on('connect', function () {
        server.write('username ' + username + '\n')
      })
    } else {
      server.write('nousername' + '\n')
    }

    server.on('data', (data) => {
      let command = data.toString().substr(0, 5).toLowerCase()
      let arr = data.toString().split('|')
      // if (command === 'msg |') {
      //   this.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' <' + arr[2].substr(1, (arr[2].length - 2)) + '>' + arr[3])
      // } else
      if (command === 'con |') {
        this.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' ' + arr[2].substr(1, (arr[2].length - 1)) + ' has joined the chat!')
      } else if (command === 'dis |') {
        this.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' ' + arr[2].substr(1, (arr[2].length - 1)) + ' has left the chat!')
      }else {
        let msgp = JSON.parse(data.toString())
        this.log('[' + msgp.msg.date + ']' + ' <' + msgp.msg.un + '>' + ' ' + msgp.msg.mes)
      //  this.log(data.toString())
      }
    })

    server.on('close', () => {
      this.log('disconnected from server :(')
      cli.delimiter('connected:')
      cli.ui.refresh()
    })
  })
  .action(function (command, callback) {
    if (command === '/quit') {
      server.write('quit | ' + '\n')
      server.end()
      callback()
    } else {
      let x = { 'msg': [
        {'un': username, 'mes': command }
      ]}
      server.write(JSON.stringify(x) + '\n')
      callback()
    }
  })

export default cli
