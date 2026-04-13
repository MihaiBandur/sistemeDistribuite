import sys
from PyQt6.QtWidgets import QApplication
from mq_communication import RabbitMqListener
from chelner_windown import ChelnerWindow


def main():
    app = QApplication(sys.argv)

    mq_client = RabbitMqListener(on_response_callback=None)

    window = ChelnerWindow(mq_client=mq_client)
    window.show()

    sys.exit(app.exec())


if __name__ == '__main__':
    main()