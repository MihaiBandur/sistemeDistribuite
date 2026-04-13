import sys
import random
import string
from PyQt6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QLabel,
    QPushButton, QTextEdit, QComboBox, QApplication, QGroupBox
)
from PyQt6.QtCore import pyqtSignal, QObject

def generate_chelne_id() -> str:
    d1 = random.randint(10, 99)
    l1 = ''.join(random.choices(string.ascii_uppercase, k=2))
    d2 = random.randint(10, 99)
    l2 = random.choice(string.ascii_uppercase)
    return f"Chelner{d1}{l1}{d2}{l2}"


class ResponseSignal(QObject):
    received = pyqtSignal(dict)

class ChelnerWindow(QWidget):
    def __init__(self, mq_client):
        super().__init__()
        self.mq_client = mq_client
        self.chelner_id = generate_chelne_id()

        self.response_signal = ResponseSignal()
        self.response_signal.received.connect(self._display_response)
        self.mq_client.on_response_callback = self._on_response_thread_safe
        self._build_ui()

    def _build_ui(self):
        self.setWindowTitle(f"Restaurant - {self.chelner_id}")

        self.setMinimumWidth(500)

        main_layout = QVBoxLayout()

        id_label = QLabel(f"<b>Chelner ID:</b> {self.chelner_id}")
        main_layout.addWidget(id_label)

        order_group = QGroupBox("Trimite Comanda")
        order_layout = QHBoxLayout()

        self.order_combo = QComboBox()
        for i in range(1, 6):
            self.order_combo.addItem(f"Comanda #{i}", i)

        send_btn = QPushButton("Trimite la Bucatar")
        send_btn.clicked.connect(self._send_order)
        send_btn.setStyleSheet("background-color: #4CAF50; color: white; padding: 6px;")

        order_layout.addWidget(QLabel("Selecteaza comanda:"))
        order_layout.addWidget(self.order_combo)
        order_layout.addWidget(send_btn)
        order_group.setLayout(order_layout)
        main_layout.addWidget(order_group)

        log_group = QGroupBox("Activitate")
        log_layout = QVBoxLayout()
        self.log_area = QTextEdit()
        self.log_area.setReadOnly(True)
        self.log_area.setPlaceholderText("Comenzile si raspunsurile vor aparea aici...")
        log_layout.addWidget(self.log_area)
        log_group.setLayout(log_layout)
        main_layout.addWidget(log_group)

        clear_btn = QPushButton("Sterge Log")
        clear_btn.clicked.connect(self.log_area.clear)
        main_layout.addWidget(clear_btn)

        self.setLayout(main_layout)
        self._log(f"Chelner {self.chelner_id} pornit. Gata de comenzi!")

    def _send_order(self):
        order_id = self.order_combo.currentData()
        order = {
            "order_id": order_id,
            "chelner_id": self.chelner_id,
            "bucatar_id": None,
            "status": "PENDING",
            "timestamp": __import__('time').time()
        }
        self._log(f">> Trimit Comanda #{order_id} catre bucatari...")
        self.mq_client.send_order(order)

    def _on_response_thread_safe(self, response: dict):
        self.response_signal.received.emit(response)


    def _display_response(self, response: dict):
        order_id = response.get('order_id')
        bucatar = response.get('bucatar_id', 'N/A')
        status = response.get('status', 'N/A')
        chelner = response.get('chelner_id', '')

        if chelner == self.chelner_id:
            self._log(
                f"<< Raspuns Comanda #{order_id}: "
                f"Status={status} | Bucatar={bucatar}"
            )

    def _log(self, message: str):
        self.log_area.append(message)