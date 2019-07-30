import os
from flask import Flask
from controller import main, pwa

webapp = Flask(
    __name__,
    instance_relative_config=True,
    static_url_path=''
)

webapp.config.from_mapping(
    SECRET_KEY=os.environ.get('SECRET_KEY') or 'you-will-never-guess',
    # SQLALCHEMY_DATABASE_URI=os.environ.get('DATABASE_URL') or 'sqlite:///' + os.path.join(app.instance_path, 'app.db'),
    # SQLALCHEMY_TRACK_MODIFICATIONS=False,
)


webapp.register_blueprint(main.bp)
webapp.register_blueprint(pwa.bp)

webapp.run(host='localhost', port=5000, debug=True)