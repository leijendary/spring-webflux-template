FROM alpine:3
WORKDIR /app
COPY build/native/nativeCompile/webflux-template .
ENTRYPOINT ["sh", "-c", "./webflux-template"]
