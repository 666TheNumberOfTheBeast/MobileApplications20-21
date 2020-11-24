from flask         import Flask
from flask_restful import Api, Resource, reqparse

parserGet  = reqparse.RequestParser()
parserPost = reqparse.RequestParser()

parserGet.add_argument("req", type=int, required=True)
parserGet.add_argument("who", type=int, required=True)

parserPost.add_argument("req", type=int, required=True)
parserPost.add_argument("who", type=int, required=True)
parserPost.add_argument("move", type=int, required=True)

# Variables for the server state
STATE_INIT = 0
STATE_WAIT = 1
STATE_PLAY = 2

# Variables for the clients requests
NEW_GAME = 0
POLLING  = 1
NEW_MOVE = 2

# Variables for the two players
UNDEFINED = -1
PLAYER0   = 0
PLAYER1   = 1

state = STATE_INIT
LAST_MOVE0 = UNDEFINED
LAST_MOVE1 = UNDEFINED

class TicTacToeServer(Resource):

    def get(self):
        global state, LAST_MOVE0, LAST_MOVE1

        args = parserGet.parse_args()

        # Check if new game request when the server is in the init state
        if state == STATE_INIT and args.req == NEW_GAME:
            state = STATE_WAIT

            # Reset last move variables
            LAST_MOVE0 = UNDEFINED
            LAST_MOVE1 = UNDEFINED

            # Return the player ID to the user who starts a new game
            return { "error": False, "who": PLAYER0, "message": "NEW GAME CREATED", "state": state }

        # Check if new game request when the server is in the wait state
        elif state == STATE_WAIT and args.req == NEW_GAME:
            # Check if new game request from a different user than before
            if args.who != PLAYER0:
                state = STATE_PLAY
                return { "error": False, "who": PLAYER1, "message": "NEW GAME CREATED", "state": state }

        # Check if polling request when the server is in the play state
        elif state == STATE_PLAY and args.req == POLLING:
            if args.who == PLAYER0:
                return { "error": False, "who": PLAYER1, "message": "POLLING MESSAGE", "move": LAST_MOVE1, "state": state }
            elif args.who == PLAYER1:
                return { "error": False, "who": PLAYER0, "message": "POLLING MESSAGE", "move": LAST_MOVE0, "state": state }

        return { "error": True }


        #if args.req == NEW_GAME:
        #    LAST_MOVE0 = -1
        #    LAST_MOVE1 = -1
        #    return { "error": False, "message": "NEW GAME CREATED", "state": 0 }

        #elif args.req == POLLING:
        #    if args.who == PLAYER0:
        #        return { "error": False, "message": "POLLING MESSAGE", "state": 0, "move": LAST_MOVE1, "who": PLAYER1 }
        #    elif args.who == PLAYER1:
        #        return { "error": False, "message": "POLLING MESSAGE", "state": 0, "move": LAST_MOVE0, "who": PLAYER0 }

        #return { "error": True }

    def post(self):
        global state, LAST_MOVE0, LAST_MOVE1

        args = parserPost.parse_args()

        if state == STATE_PLAY and args.req == NEW_MOVE:
            if args.who == PLAYER0:
                LAST_MOVE0 = args.move
                return { "error": False, "message": "NEW MOVE", "state": state }
            elif args.who == PLAYER1:
                LAST_MOVE1 = args.move
                return { "error": False, "message": "NEW MOVE", "state": state }

        return { "error": True }


        #if args.req == NEW_MOVE:
        #    if args.who == PLAYER0:
        #        LAST_MOVE0 = args.move
        #        return { "error": False, "message": "NEW MOVE", "status": 0 }
        #    elif args.who == PLAYER1:
        #        LAST_MOVE1 = args.move
        #        return { "error": False, "message": "NEW MOVE", "status": 0 }

        #return { "error": True }


app = Flask(__name__)
api = Api(app)
api.add_resource(TicTacToeServer, "/")

if __name__ == "__main__":
    app.run(host="0.0.0.0")
