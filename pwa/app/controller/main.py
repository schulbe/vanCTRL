from flask import (
    Blueprint, render_template
)

bp = Blueprint('main', __name__)


# @bp.route('/')
# def index():
#     return render_template('main/index.html',
#                            title='Flask-PWA')

@bp.route('/')
def root():
    return render_template('layout.html',
                           title='ctrl_van')


@bp.route('/light')
def ctrl_light():
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