try:
    import vim
except ImportError: 
    # hopefully running in unit test!
    pass

import os, platform, urllib2, httplib, socket, json, re, time, inspect

class IVClient(object):

    """Manages interactions with IntelliVim server"""

    ERROR_NO_SERVER = "{'error': 'No IntelliVim server running'}"
    ERROR_UNEXPECTED = "{'error': 'Unexpected server error'}"
    ERROR_SERVER_CONNECTION = "{'error': 'Could not connect to IntelliVim Server'}"
    ERROR_TIMEOUT = "{'error': 'Timeout contacting IntelliVim Server'}"

    # we now only execute when we know there's
    #  a server, so longer timeouts are safe
    TIMEOUT = 5.0

    _instance = None

    def __init__(self):
        """Private constructor; prefer the #get() singleton accessor """

        self.port = 0
        self._lastUpdate = None

    def _detectPort(self):
        """First checks the buffer, then a global var,
        then our last-used port, then finally looks on disk
        :returns: The port number to use, or <= 0 if none found

        """
        if vim.current.buffer.vars.has_key('intellivim_port'):
            return vim.current.buffer.vars['intellivim_port']

        if vim.vars.has_key('intellivim_port'):
            return vim.vars['intellivim_port']

        if self.port != 0:
            return self.port

        return 0

    def _makeRequest(self, type, doc, timeout=None):
        
        if timeout is None:
            timeout = self.TIMEOUT

        port = self._detectPort()
        if port <= 0:
            return IVClient.ERROR_NO_SERVER
            
        try:
            url = 'http://localhost:' + str(port) + '/' + type
            req = urllib2.Request(url, \
                data=json.dumps(doc), \
                headers={'Content-Type':'application/json'})
            res = urllib2.urlopen(req, timeout=timeout)
            if res.getcode() == 204:
                return True # indicate success somehow

            return res.read()
        except urllib2.HTTPError, error:
            return json.dumps({'error': error.read()})
        except urllib2.URLError:
            # probably, connection refused
            IVClient._instance = None
            return IVClient.ERROR_SERVER_CONNECTION
        except httplib.BadStatusLine:
            return IVClient.ERROR_UNEXPECTED
        except socket.timeout:
            return IVClient.ERROR_TIMEOUT

    def _execute(self, command, timeout=None):
        """Execute a command

        :command: Command Dictionary
        :returns: Result Dictionary

        """
        return self._makeRequest('command', command, timeout)

    @classmethod
    def get(cls):
        """Singleton accessor
        :returns: the global Njast instance

        """
        if cls._instance is not None:
            return cls._instance

        newInstance = IVClient()
        cls._instance = newInstance
        return newInstance

    @classmethod
    def execute(cls, command, timeout=None):
        """Shortcut for IVClient.get()._execute()
        """
        return cls.get()._execute(command, timeout)
