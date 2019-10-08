# How I built this

Built with :heart: using

[![Clojars Project](https://img.shields.io/clojars/v/cli/lein-template.svg)](https://clojars.org/cli/lein-template)

```
lein new cli app
```

# Generate test data

Quick ways to generate a timed events

## Nginx

```
docker run -p 80:80 --read-only -v $(pwd):/var/cache/nginx -v /tmp/nginx:/var/run nginx | awk '{print $4 " " $9; fflush(stdout)}' | lein run
```

## Python

```
python -u -m SimpleHTTPServer 3000 2>&1 | awk '{print $4 ":" $5 " " $9; fflush(stdout)}' | lein run
```
