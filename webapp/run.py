import configparser
import os
from flask import Flask
from flask import render_template, request
from control_interface import Controller

try:
    from webapp.blueprints import pwa
except ModuleNotFoundError:
    from blueprints import pwa

app = Flask(
    __name__,
    instance_relative_config=True,
    static_url_path=''
)

app.config.from_mapping(
    SECRET_KEY=os.environ.get('SECRET_KEY') or 'you-will-never-guess'
)


config = configparser.ConfigParser(os.environ)
config.read('config.ini')

controller = Controller(config['ROUTES']['ctrl_host'], config['ROUTES']['ctrl_port'])


@app.route('/')
def root():
    return render_template('layout.html',
                           title='ctrl_van')


@app.route('/light', methods=['GET', 'POST'])
def ctrl_light():
    if request.method == 'POST':
        if "backLightSwitch" in request.form:
            controller.switch_light('back')
        if "frontLightSwitch" in request.form:
            controller.switch_light('front')

    light_status = controller.get_light_status()

    return render_template('ctrl_light.html',
                           title='lights')


@app.route('/battery')
def ctrl_battery():
    return render_template('layout.html',
                           title='battery')


@app.route('/fridge')
def ctrl_fridge():
    return render_template('layout.html',
                           title='fridge')


@app.route('/music')
def ctrl_music():
    return render_template('layout.html',
                           title='music')


app.register_blueprint(pwa.bp)



app.run(host=config['ROUTES']['ui_host'],
        port=config['ROUTES']['ui_port'],
        debug=True)
