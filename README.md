# What

Lexisoup is an Android and a web app, which gives you a "soup" of lexical data about foreign words:
- Words' translations
- Explanations
- Forms
- Synonyms
- Usages examples

Lexisoup has no data of its own.
Instead, in the hope of bringing a better understanding of the word, lexisoup brings together data from the following sources:
- PanLex
- Kaikki (Wiktionary)
- Tatoeba
- Wortschatz Leipzig
- ChatGPT

Some of the sources are queried directly by the Android app, some sources are queried from the lexisoup server, which can be found here: https://github.com/blazern/lexisoup-server
The web-app queries all the services through the lexisoup server (because of CORS).

# Overall project architecture

```mermaid
flowchart LR

%% Apps
android["Android app (KMP)"]
web["Web app (KMP)"]

%% Cloud
subgraph vps["Cloud"]
  direction TB
  nginx["Nginx"]
  subgraph docker["Docker"]
    server["Rust server<br/>GraphQL: /api<br/>REST: /XXX"]
    panlex["PanLex SQLite<br/>(DB file)"]
  end
end

%% External services (forced column)
subgraph ext["External services"]
  direction TB
  tatoeba["tatoeba.org"]
  kaikki["kaikki"]
  chatgpt["ChatGPT"]
  wortschatz["Wortschatz Leipzig"]
end

%% Traffic
android -->|PanLex and ChatGPT GraphQL requests| nginx
web -->|All services requests| nginx

nginx -->|GraphQL + REST| server

server -->|read translations +<br/>suggestions| panlex

%% External integrations (block-level)
android --> ext
server -->|HTTP reverse proxy| ext
```