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
      if (command === 'msg |') {
        console.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' <' + arr[2].substr(1, (arr[2].length - 2)) + '> says:' + arr[3])
      } else if (command === 'con |') {
        console.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' ' + arr[2].substr(1, (arr[2].length - 1)) + ' has joined the chat!')
      } else if (command === 'dis |') {
        console.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' ' + arr[2].substr(1, (arr[2].length - 1)) + ' has left the chat!')
      }else {
        this.log(data.toString())
      }
    })

    server.on('end', () => {
      this.log('disconnected from server :(')
    })
  })
  .action(function (command, callback) {
    if (command === 'exit') {
      server.end()
      callback()
    } else {
      server.write(command + '\n')
      callback()
    }
  })

export default cli
