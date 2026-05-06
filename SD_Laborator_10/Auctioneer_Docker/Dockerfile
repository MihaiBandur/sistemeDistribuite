FROM python:3.11-alpine

# instalare modul kafka-python
RUN pip install kafka-python-ng

# adaugare fisier sursa pentru microserviciul Auctioneer
WORKDIR /auctioneer
ADD Auctioneer.py $WORKDIR

# comanda de executie este: python <nume_fisier>.py
CMD ["python3", "Auctioneer.py"]