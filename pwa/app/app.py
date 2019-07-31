import os
from flask import Flask
from controller import main, pwa


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

app.register_blueprint(main.bp)
app.register_blueprint(pwa.bp)
