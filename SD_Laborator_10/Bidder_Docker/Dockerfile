FROM python:3.11-alpine

RUN pip install kafka-python-ng

WORKDIR /bidder

ADD Bidder.py $WORKDIR

CMD ["python3", "Bidder.py"]
