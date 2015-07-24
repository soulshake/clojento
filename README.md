# clojento

Magento Tools

## Usage

```bash
lein deps
lein repl
```

```clojure
(go)
(clojento.config/config (:configurator system) :db)
(clojento.magento.db/fetch (:db system) "SELECT * FROM core_website")
(reset)
```

## License

Copyright (C) 2011 Jean-Luc Geering

Distributed under the MIT License (MIT).
