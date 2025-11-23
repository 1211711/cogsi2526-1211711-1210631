# COGSI CA5 PART2 - Detailed Docker Compose Report

This document is part of the _COGSI_ (Configuration and Systems Management) class from the Software Engineering master’s course at _ISEP_ (Instituto Superior de Engenharia do Porto).

This goal of this assignment was to containerize the Gradle version of the Building REST Services with Spring application and run it together with an H2 database using Docker Compose.

---

## 1. Database H2 Base Configuration

The db service runs an H2 database in TCP server mode and exposes a Web Console for administration.

```dockercompose
  db:
    image: joaosousa00/h2db
    ports:
      - "81:81"
      - "1521:1521"
    hostname: db
    container_name: db
    environment:
      - H2_OPTIONS=-ifNotExists -web -webAllowOthers -tcp -tcpAllowOthers
```

1. Image: oscarfonts/h2 (latest)
    1. Chosen because it provides a preconfigured H2 server with TCP and Web support, simplifying deployment.
    2. The image was pulled from Docker Hub, then tagged and pushed to our Docker Hub repository `joaosousa00/h2db` mainly to:
        - Ensure reproducibility
        - Allow version control
2. Ports Exposed:
    1. `1521` → JDBC connections for Spring Boot
    2. `81` → H2 Web Console for monitoring
3. Container Hostname: `db`
    1. Ensures predictable connectivity from the web container
4. H2 Options: `-ifNotExists -web -webAllowOthers -tcp -tcpAllowOthers`
    1. Enables new database creation if missing
    2. Allows external TCP and Web connections from other containers
5. Container Name: `db`
    1. Simplifies container management and troubleshooting
  
## 2. Web Base Configuration

The web service hosts the Spring Boot Tut REST application, exposing its API to the host machine and connecting to the db container.

```dockercompose
  web:
    image: joaosousa00/tut-rest:part2
    ports:
      - "8000:5000"
```

1. Image: `joaosousa00tut-rest:part2`
    1. A new image using the dockerfile created in the previous assignment was tagged, created and pushed to dockher hub
2. Ports: `5000` internal mapped to `8000` on the host

## 3. Database Healthcheck and Web Dependency

To avoid startup errors where the web application connects to an unavailable database, a health check was added to the db container. 
Besides this a condition on the web container was also addressed, making sure web only startups when the db is healthy.

```dockercompose
services:
  db:
    ...
    healthcheck:
      test: [ "CMD", "curl", "-f", "http://localhost:81" ]
      interval: 30s
      retries: 3
      start_period: 10s
      timeout: 10s
    networks:
      app_network:
    ...

  web:
    depends_on:
      db:
        condition: service_healthy
    networks:
      app_network:

networks:
  app_network:
```

1. Healthcheck command: `[ "CMD", "curl", "-f", "http://localhost:81" ]`
    1. Periodically checks that the H2 Web Console is responding
    2. Timing parameters:
        - `interval: 30s, timeout: 10s, retries: 3, start_period: 10s`
2. Web dependency: `depends_on: db: condition: service_healthy`
    1. Guarantees that the Spring Boot application only starts once the database is ready, avoiding connection failures
    2. Reason: ensures reliable startup sequencing and avoids race conditions between containers
3. Network setup
    1. All services are connected to a custom Docker network to enable hostname-based communication.
    2. Allows the `web` container to connect to the `db` container using `db` as hostname
    3. Isolates the application from other Docker containers
    4. Ensures predictable container-to-container communication
  
![Db connection to web](img/dbConnectionToWeb.png)

![Web running](img/webRunningCmd.png)

## 4. Database Volume and Backup

Persistence is critical to avoid losing data when containers restart or are rebuilt.

```dockercompose
services:
  db:
    ...
    volumes:
      - db_data:/opt/h2-data
    ...

  backup:
    image: alpine
    command: [ "sh", "-c", "cp -r /opt/h2-data /backup" ]
    volumes:
      - db_data:/opt/h2-data:ro
      - ./backup:/backup
    networks:
      - app_network

volumes:
  db_data:
```

1. Volume: `db_data`
    1. Mounted path: `/opt/h2-data` inside the container
2. Backup Service: copies `/opt/h2-data` to the host `./backup` to demonstrate persistence
    1. Importance: ensures durable storage, data survives container lifecycle changes, and facilitates backups
    2. Uses `alpine` since it's an image ideal for small, temporary tasks, due to it being very lightweight
    3. Copies the database files from the named volume `db_data` to a folder on the host machine `./backup`
         - Note that the volume was `ro` specified, which means readonly permission
    5. Ensures persistent backup outside of Docker containers, allowing data recovery even if containers are removed or rebuilt
    6. Also connected to the existing network
  
![Backup host folder](img/backupHostFolder.png)

## 5. Environment File (web.env)

Environment variables allow the web container to connect to the database without hardcoding credentials.

```env
DB_URL=jdbc:h2:tcp://db:1521/h2-data
DB_USERNAME=sa
DB_PASS=
```

```dockercompose
  web:
      ...
      env_file:
        - web.env
      ...
```

1. `web.env` file
    1. Contains the variable values to be populated 
2. Loaded in Docker Compose via `env_file`
    1. Injected into Spring Boot via `application.properties`
3. `application.properties` file update
    1. The `application.properties` was updated in order to support the new variables:
        - `spring.datasource.url=${DB_URL:jdbc:h2:tcp://192.168.33.11:9090/database}`
    2. This allows the variable to be overrided while still maintaining a default value
    3. It allows easy reconfiguration for different environments and avoids storing secrets in the Dockerfile

## 6. Images on Docker Hub

Both web and db images were tagged and pushed into docker hub, as already previously mentioned

Tagging:

- `docker tag tut-rest:part2 joaosousa00/tut-rest:part2`
- `docker tag h2db:latest joaosousa00/h2db:latest`

Push to Docker Hub:

- `docker push joaosousa00/tut-rest:part2`
- `docker push joaosousa00/h2db:latest`

![Docker hub](img/dockerHub.png)

---

## **Alternative Solutions**

While this assignment uses _**Docker Compose**_ to run multi-container environments, several alternative technologies exist that provide similar capabilities for packaging, deploying, and orchestrating containerized applications. Among the most relevant options in the container ecosystem are **_Podman Compose_** and **_Kubernetes_**.

Both aim to manage multiservice deployments and ensure reproducible environments, but they differ significantly in architecture, scope, and operational complexity.

For this module, we will focus on **Kubernetes** as the alternative solution.


### **Kubernetes**

_Kubernetes_ is a container orchestration platform designed to **deploy**, **scale**, and **manage applications** across distributed environments. Unlike _Docker Compose_ and _Podman Compose_, which target local or small-scale multi-container setups, _Kubernetes_ provides a **full orchestration layer** suitable for production-grade, cloud-native infrastructure.

Rather than relying on the _Docker Engine_, _Kubernetes_ uses **container runtimes** compatible with the _**Container Runtime Interface (CRI)**_, such as **_containerd_** or **_CRI-O_**, making it independent of the Docker ecosystem. Applications are defined declaratively through **YAML manifests**, and _Kubernetes_ ensures their desired state through mechanisms such as **automated scheduling**, **self-healing**, **rolling updates**, and **service discovery**.

Kubernetes organizes applications using several core abstractions:

* **Pods**, the smallest deployable units that encapsulate one or more containers
* **Deployments**, which manage application lifecycle, replicas, and updates
* **Services**, which expose network-accessible endpoints with stable addresses
* **ConfigMaps and Secrets**, which separate configuration and sensitive data from container images

#### 1. Implementation Steps

_Kubernetes_ encourages a modular and declarative configuration approach, splitting the implementation into four separate files which improves **clarity** and **maintainability**.

* `namespace.yaml` defines the isolated environment (part2) where all resources will operate. Keeping it separate ensures the namespace can be created, reused, or deleted independently of the application logic.

* `database.yaml` contains all database-related resources: the Deployment, Service, and PersistentVolumeClaim. Grouping these in a dedicated file makes the data layer self-contained and easier to manage, update, or replace without affecting other components.

* `web.yaml` includes the definitions for the web application, such as the Secret for environment variables, the Deployment, and the Service. This separation allows the application logic and configuration to evolve independently from the database or backup routines.

* `backup.yaml` defines the one-time backup job responsible for copying database files. Because `Jobs` represent short-lived tasks rather than long-running services, isolating this file avoids mixing transient and persistent workloads.

This modular structure more accurately reflects the Kubernetes operational model, where each resource is treated as an **independent**, **declarative object**.

1. **namespace.yaml** 

* Defines a logical, isolated environment within the Kubernetes cluster.
* Groups all application components (Web, Database, Backup) under a single namespace called _**part2**_.
* Simplifies management, cleanup, and organization of related resources.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: part2

```

2. **database.yaml**

- **Deployment (db)**
    - Runs a Pod containing the H2 database container.
    - Defines environment variables (similar to Docker Compose `environment:`).
    - Mounts a PersistentVolumeClaim at `/opt/h2-data` to persist DB data.

- **Service (db)**
    - Exposes ports 81 and 1521 inside the cluster.

- **PersistentVolumeClaim**
    - Kubernetes replacement for the Docker Compose named volume.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: db
  namespace: part2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: db
  template:
    metadata:
      labels:
        app: db
    spec:
      containers:
        - name: db
          image: joaosousa00/h2db
          env:
            - name: H2_OPTIONS
              value: "-ifNotExists -web -webAllowOthers -tcp -tcpAllowOthers"
          ports:
            - containerPort: 81
            - containerPort: 1521
          volumeMounts:
            - name: db-data
              mountPath: /opt/h2-data
      volumes:
        - name: db-data
          persistentVolumeClaim:
            claimName: db-pvc

---
apiVersion: v1
kind: Service
metadata:
  name: db
  namespace: part2
spec:
  selector:
    app: db
  ports:
    - name: web
      port: 81
      targetPort: 81
    - name: tcp
      port: 1521
      targetPort: 1521

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: db-pvc
  namespace: part2
spec:
  accessModes:
    - ReadWriteOnce
```

#### 3. **web.yaml**

- **Deployment (web)**
    - Runs the web application Pod.
    - Injects configuration via environment variables on a file `web-env`.
    - Defines container ports similar to Docker Compose `ports` (but internal).

- **Service (web)**
    - Exposes the web application inside the cluster.
    - Allows communication with database.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
  namespace: part2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: web
  template:
    metadata:
      labels:
        app: web
    spec:
      containers:
        - name: web
          image: joaosousa00/tut-rest:part2
          envFrom:
            - secretRef:
                name: web-env
          ports:
            - containerPort: 5000

---
apiVersion: v1
kind: Service
metadata:
  name: web
  namespace: part2
spec:
  type: NodePort
  selector:
    app: web
  ports:
    - port: 5000
      targetPort: 5000
      nodePort: 30080
```


#### 4. **backup.yaml**

- **Job (Backup)**
    - Executes a one-time Pod to copy database files.
    - Designed for batch operations, not long-running services.

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: db-backup
  namespace: part2
spec:
  template:
    spec:
      containers:
        - name: backup
          image: alpine
          command: ["sh", "-c", "cp -r /opt/h2-data /backup"]
          volumeMounts:
            - name: db-data
              mountPath: /opt/h2-data
            - name: backup-out
              mountPath: /backup
      restartPolicy: Never
      volumes:
        - name: db-data
          persistentVolumeClaim:
            claimName: db-pvc
        - name: backup-out
          hostPath:
            path: /tmp/backup
            type: DirectoryOrCreate
```

#### 5. Commands

| Docker Compose              | Kubernetes Equivalent                                                                                                                                                                  | Observations                                                                                              |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `docker-compose up --build` | `kubectl apply -f namespace.yaml` <br>`kubectl apply -f database.yaml` <br>`kubectl create secret generic web-env --from-env-file=web.env -n app-demo` <br>`kubectl apply -f web.yaml` | Kubernetes does **not build images automatically**; images must be pre-built and available in a registry. |
| `docker-compose down`       | `kubectl delete -f web.yaml` <br>`kubectl delete -f database.yaml` <br>`kubectl delete -f backup.yaml` <br>`kubectl delete -f namespace.yaml`                                          | Deletes all resources; PersistentVolumeClaims may be preserved depending on their reclaim policy.         |
| `docker-compose logs`       | `kubectl logs <pod-name> -n part2`                                                                                                                                                     | Logs are per pod, not aggregated per service.                                                             |

### Conclusion

Both **_Docker Compose_** and **_Kubernetes_** are tools for managing containerized applications, enabling developers to define multi-service deployments and maintain consistent runtime environments. However, they differ significantly in their architecture, operational model, and scalability.

**_Docker Compose_** follows a **single-host, simple orchestration model**: all services are defined in a single YAML file, networks and volumes are created automatically, and containers are started with minimal configuration. Its simplicity makes it lightweight, easy to learn, and ideal for **local development, testing, or small-scale projects**. Docker Compose focuses on accessibility and speed, providing a straightforward way to bring up multi-container applications without complex setup.

**_Kubernetes_**, in contrast, is a **distributed, declarative orchestration platform**. Applications are deployed across one or more nodes using Pods, Deployments, Services, and PersistentVolumes. Kubernetes continuously ensures the **desired state**, automatically handling scaling, self-healing, rolling updates, and service discovery. While its learning curve is steeper and configuration more verbose, Kubernetes excels in **production-grade, cloud-native environments**, where high availability, fault tolerance, and scalability are essential.

While **_Docker Compose_** prioritizes **simplicity, speed, and ease of use**, **Kubernetes provides** greater **robustness, scalability, and operational automation**. _Docker Compose_ is suitable for smaller or academic projects, whereas Kubernetes becomes the more powerful solution for long-term, production-oriented deployments.

---

## Developers

| Name       |  Number | Evaluation |
| ---------- | :-----: | :--------: |
| João Sousa | 1210631 |    100%    |
| João Brito | 1211711 |    100%    |
