## Optional

O unealta utila in opinia mea este `tmux`. Ea va permite sa porniti mai multe ferestre de terminal si sa le dati split dupa bunul plac. Un [video](https://www.youtube.com/watch?v=vtB1J_zCv8I) despre asta. Pentru a automatiza tmux exista o alta unealta numita tmuxp. Aceasta porneste automat tmux impreuna cu niste comenzi specificate intr-un config yaml.

```bash
sudo apt update
sudo apt install tmux -y
pip install tmuxp
```

```bash
tmuxp load ./run_all.yaml
```


Modificati fisierul yaml pentru a activa propriul vostru virtual env de python.
