from flask import Blueprint, render_template, request
from control_interface import Controller
bp = Blueprint('main', __name__)

controller = Controller()


# @bp.route('/')
# def index():
#     return render_template('main/index.html',
#                            title='Flask-PWA')



@bp.route('/')
def root():
    return render_template('layout.html',
                           title='ctrl_van')


@bp.route('/light', methods=['GET', 'POST'])
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


@bp.route('/battery')
def ctrl_battery():
    return render_template('layout.html',
                           title='battery')


@bp.route('/fridge')
def ctrl_fridge():
    return render_template('layout.html',
                           title='fridge')


@bp.route('/music')
def ctrl_music():
    return render_template('layout.html',
                           title='music')