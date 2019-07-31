import os
import configparser
from flask import Flask, request, jsonify


config = configparser.ConfigParser(os.environ)
config.read('config.ini')

if os.environ['USER'] == 'pi':
    try:
        from control_app.controller import Controller
    except ModuleNotFoundError:
        from controller import Controller

else:
    try:
        from control_app.controller_STUMP import Controller
    except ModuleNotFoundError:
        from controller_STUMP import Controller

controller = Controller(config['GPIOS'])

app = Flask(
    __name__,
    instance_relative_config=True,
    static_url_path=''
)

@app.route('/cmd', methods=['POST'])
def command():
    try:
        cmd = request.get_json()
        if cmd['target'] == 'lights':
            if cmd['cmd'] == 'switch':
                controller.switch_light(cmd['light_position'])
        elif cmd['target'] == 'fridge':
            pass
        elif cmd['target'] == 'battery':
            pass
        elif cmd['target'] == 'music':
            pass
    except:
        return '500'

    return '200'


@app.route('/info', methods=['POST'])
def info():
    try:
        req = request.get_json()
        if req['target'] == 'lights':
            return jsonify(controller.get_light_status()), 200
        elif req['target'] == 'fridge':
            pass
        elif req['target'] == 'battery':
            pass
        elif req['target'] == 'music':
            pass
    except:
        return '500'

    return '200'


app.run(host=config['ROUTES']['ctrl_host'],
        port=config['ROUTES']['ctrl_port'],
        debug=True)
