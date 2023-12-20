FROM gradle:jdk17-jammy
WORKDIR /app
COPY . /app

RUN gradle --no-daemon --warning-mode all --console=plain buildDependents

EXPOSE 3000
CMD ["sh", "start.sh"]