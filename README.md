# clojento

[![Build Status](https://travis-ci.org/jlgeering/clojento.svg)](https://travis-ci.org/jlgeering/clojento)

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

running tests in the repl:

```clojure
(autotest)
```

## License

Copyright (C) 2011 Jean-Luc Geering

Distributed under the MIT License (MIT).
