import os
from flask import Flask
from webapp.blueprints import user_interfaces, pwa
from flask import Blueprint, render_template, request
from control_interface import Controller

app = Flask(
    __name__,
    instance_relative_config=True,
    static_url_path=''
)

app.config.from_mapping(
    SECRET_KEY=os.environ.get('SECRET_KEY') or 'you-will-never-guess',
    # SQLALCHEMY_DATABASE_URI=os.environ.get('DATABASE_URL') or 'sqlite:///' + os.path.join(app.instance_path, 'app.db'),
    # SQLALCHEMY_TRACK_MODIFICATIONS=False,
)


controller = Controller()


@app.route('/')
def root():
    return render_template('layout.html',
                           title='ctrl_van')


@app.route('/light', methods=['GET', 'POST'])
def ctrl_light():
    # if request.method == 'POST':
    #     if "backLightSwitch" in request.form:
    #         g.controller.switch_light('back')
    #     if "frontLightSwitch" in request.form:
    #         g.controller.switch_light('front')
    #
    # light_status = g.controller.get_light_status()

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
