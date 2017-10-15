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

    //Are we connecting with a username, or are we gonna be assigned an anonymous name?
    if (username) {
      server.on('connect', function () {
        server.write('username ' + username + '\n')
      })
    } else {
      server.write('nousername' + '\n')
    }

    server.on('data', (data) => {
      //Commands will always be the first 4 letters, so we are going to parse that, and split anything else into an array
      let command = data.toString().substr(0, 5).toLowerCase()
      let arr = data.toString().split('|')

      if (command === 'con |') {
        //Yay! A new user!
        this.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' ' + arr[2].substr(1, (arr[2].length - 1)) + ' has joined the chat!')
      } else if (command === 'dis |') {
        //Looks like someone disconnected.
        this.log('[' + arr[1].substr(1, (arr[1].length - 2)) + ']' + ' ' + arr[2].substr(1, (arr[2].length - 1)) + ' has left the chat!')
      }else {
        //It's a message! We're gonna parse the date, username and message.
        let msgp = JSON.parse(data.toString())
        this.log('[' + msgp.msg.date + ']' + ' <' + msgp.msg.un + '>' + ' ' + msgp.msg.mes)
      }
    })

    //When we disconnect from the server, it gets a little sad :(
    server.on('close', () => {
      this.log('disconnected from server :(')
      cli.delimiter('connected:')
      cli.ui.refresh()
    })
  })

  /*
    This functions as two things. One, our command quit, which disconnections as our server, otherwise, we are sending a typed message into the chat room.
  */
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
