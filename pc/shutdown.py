import platform
import atexit

def register_function(f):
    if "Windows" == platform.system():
        try:
            import win32api
            win32api.SetConsoleCtrlHandler(f, True)
        except Exception as e:
            print e
            print "FAILED TO REGISTER CONSOLE HANDLING"
    else:
        try:
            import signal

            def stop(sig, frame):
                print 'caught SIGTERM\n'

            def ignore(sig, frsma):
                print 'ignoring signal %d\n' % sig
                
            signal.signal(signal.SIGTERM, stop)
            signal.signal(signal.SIGHUP, ignore)
        except Exception as e:
            print e
            print "FAILED TO REGISTER SIGNAL HANDLING"

    atexit.register(f)
