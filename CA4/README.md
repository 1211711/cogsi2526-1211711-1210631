# COGSI CA4

This document is part of the _COGSI_ (Configuration and Systems Management) class from the Software Engineering master's
course at _ISEP_ (Instituto Superior de Engenharia do Porto).

The goal of this assignment was to **evolve Part 2 of CA3** by using **Ansible as a provisioner** in both virtual machines.  
This approach introduces a more modern and scalable configuration management tool, promoting the use of **idempotent automation**, **error handling**, and **infrastructure-as-code** practices.

Specifically, the task required to:
- Use **Ansible** to deploy and configure the **Spring REST application** on **host1 (web VM)**.
- Use **Ansible** to deploy and configure **H2 Database** on **host2 (db VM)**.
- Ensure all **playbooks are idempotent**, handling potential errors with `failed_when`, `ignore_errors`, and retry logic.
- Run the playbooks **twice**, ensuring the second run shows **0 or minimal changes** to confirm idempotency.

---

### 1. Virtual Machine Setup (Vagrantfile) and playbook organization

1. The Ansible provisioner was integrated directly in the `Vagrantfile` to execute specific playbooks on each VM. A global playbook was created in order to execute the tasks relevant to every deployment.
    1. ```config.vm.provision "ansible"``` defines Ansible as the provisioning tool for the VM. Other possible options include "shell" (for scripts) or "puppet".
    2. ```ansible.playbook``` specifies the path to the YAML playbook that should be executed.
    3. ```ansible.compatibility_mode = "2.0"``` ensures compatibility with newer Vagrant–Ansible integrations.
    ```ruby
    config.vm.provision "ansible" do |ansible|
        ansible.playbook = "./ansible/globalPlaybook.yml"
        ansible.compatibility_mode = "2.0"
    end
    ```

2. Besides this two other playbooks were also created, in order to separate application dependencies from the database and vice-versa
    ```ruby
    db.vm.provision "ansible" do |ansible|
        ansible.playbook = "./ansible/dbPlaybook.yml"
    end
    
    web.vm.provision "ansible" do |ansible|
        ansible.playbook = "./ansible/webPlaybook.yml"
    end
    ```

---

### 2. Global Configuration (globalPlaybook.yml)

1. This playbook applies a common base setup across all virtual machines, ensuring they share essential tools such as Git (for repository cloning) and OpenJDK 17 (for Java-based applications).
    ```ruby
    - name: Global VMs Provision
      hosts: all
      become: true
      tasks:
        - name: Install git
          apt:
            name: git
            state: present
            update_cache: yes
    
        - name: Install java
          apt:
            name: openjdk-17-jdk
            state: present
            update_cache: yes
    ```

1. Install git
   1. Git is required to clone the project repository containing the Spring REST application.
   2. Module: apt (used for managing Debian-based packages like Ubuntu).
   3. Options:
      1. name: git — specifies the package to install.
      2. state: present — ensures Git is installed. Possible values:
      3. present: installs the package if missing. 
      4. absent: removes it. 
      5. latest: upgrades it to the newest version. 
      6. update_cache: yes — refreshes the package index before installation. 
      7. yes: updates the cache to ensure the latest package metadata. 
      8. no: skips update (faster but risk of outdated metadata).

2. Install java
   1. The Spring application and H2 database both require a Java runtime environment.
   2. Options: same as above, ensuring the package openjdk-17-jdk is installed with up-to-date dependencies. 

3. Both tasks are idempotent, meaning that if Java or Git are already installed, running the playbook again will result in no changes.

### 3. Database Server Configuration (dbPlaybook.yml)

1. The database server playbook installs and configures H2 Database to run as a background service.
This setup mimics a production-ready environment with persistent data and automatic startup.

    ```ruby
    - name: Database VM Provision
      hosts: db
      become: true
      tasks:
        - name: Download H2 Database
          get_url:
            url: https://repo1.maven.org/maven2/com/h2database/h2/2.4.240/h2-2.4.240.jar
            dest: /opt/dev/h2-2.4.240.jar
    
        - name: Create shared directory for H2 database
          file:
            path: /vagrant/h2/h2db
            state: directory
            mode: '0770'
    
        - name: Create H2 server systemd service file
          copy:
            dest: /etc/systemd/system/h2db.service
            content: |
              [Unit]
              Description=H2 Database Server
              After=network.target
    
              [Service]
              ExecStart=/usr/bin/java -cp "/opt/dev/h2-2.4.240.jar" org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9092 -baseDir /vagrant/h2db -ifNotExists
              User=root
              Restart=always
    
              [Install]
              WantedBy=multi-user.target
    
        - name: Start H2 Database service
          systemd:
            name: h2db
            state: started
            enabled: true
    ```

1. Download H2 Database
   1. The H2 JAR file must be downloaded from the Maven repository to run the database server.
   2. Module: get_url (fetches files from remote URLs).
   3. Options:
      1. url: the download source.
      2. dest: local destination path.
      3. mode: optional; defines file permissions (not needed here).
      4. Idempotent behavior: If the file already exists with the same checksum, it won’t download again.
2. Create shared directory
   1. The H2 server stores its data files here. The /vagrant path is synced with the host machine.
   2. Module: file.
   3. Options:
      1. path: target directory path.
      2. state: directory: ensures the path is a directory.
      3. mode: '0770': sets Unix permissions (read/write/execute for owner and group only).
      4. Possible states: file, absent, touch, link, hard, etc.
3. Create systemd service file
   1. Allows the H2 database to start automatically and run persistently as a background service.
   2. Module: copy.
   3. Options:
      1. dest: where the file should be written.
      2. content: inline content for the service definition.
      3. Idempotent behavior: If the file already exists with the same content, it won’t overwrite.
4. Start H2 Database service
   1. Ensures that H2 starts immediately and remains active after reboots.
   2. Module: systemd.
   3. Options:
      1. name: service name.
      2. state: started — ensures the service is running.
      3. stopped: would stop the service.
      4. restarted: forces a restart.
      5. enabled: true — starts the service automatically at boot.
5. Idempotent: Only starts the service if it’s not already running.

### 4. Web Server Configuration (webPlaybook.yml)

1. This playbook configures the web VM to deploy the REST API, connect it to the H2 database, and ensure it only runs once the DB is ready.
    ```ruby
    - name: Web VM Provision
      hosts: web
      become: true
      tasks:
        - name: Check if repo directory exists
          stat:
            path: /opt/dev/cogsi2526-1211711-1210631
          register: repo_dir
    
        - name: Clone the repository
          git:
            repo: 'https://github.com/1211711/cogsi2526-1211711-1210631.git'
            dest: /opt/dev/cogsi2526-1211711-1210631
          when: not repo_dir.stat.exists
    
        - name: Update H2 database URL in application.yaml
          lineinfile:
            path: /opt/dev/cogsi2526-1211711-1210631/CA2/Part2/gradle-migration/src/main/resources/application.properties
            regexp: 'jdbc:h2:tcp://192.168.33.11:9092/database'
            line: 'jdbc:h2:tcp://192.168.33.11:9092/h2db'
    
        - name: Wait for H2 database to be ready
          wait_for:
            host: 192.168.33.11
            port: 9092
            delay: 1
            timeout: 3
            state: started
          retries: 5
          delay: 3
    ```

2. Check if repo directory exists
   1. Avoid cloning the repository multiple times.
   2. Module: stat (checks file or directory status).
   3. Options:
      1. path: location to check.
      2. Registers result in repo_dir, which is used later for conditional logic.
3. Clone the repository
   1. The application code is hosted in a GitHub repository.
   2. Module: git.
   3. Options:
      1. repo: URL of the Git repository.
      2. dest: destination path to clone into.
   4. Conditional:
      1. ```when: not repo_dir.stat.exists``` only clones if the directory doesn’t already exist.
   5. Idempotent: Ensures cloning happens only once.
4. Update H2 database URL
   1. Adjusts the Spring Boot configuration file so the application connects to the database VM.
   2. Module: lineinfile.
   3. Options:
      1. path: file to edit.
      2. regexp: regular expression to find the target line.
      3. line: replacement line.
   4. Idempotent: Only makes changes if the current line differs.
5. Wait for H2 database
   1. Ensures that the REST API starts only after the database is running.
   2. Module: wait_for.
   3. Options:
      1. host: IP address of the DB server.
      2. port: port number to check (9092 for H2).
      3. delay: seconds to wait before the first check.
      4. timeout: maximum wait time.
      5. state: started: waits until the port is open.
      6. retries and delay: retry logic for reliability.
   4. This improves provisioning resilience and avoids race conditions.

### X. Idempotency Verification

Ansible is inherently idempotent, meaning tasks only make changes if the desired state is not already met.
To verify this, the playbooks were executed twice.

TODO: PUT HERE THE PRINTSCREENS

## Alternative Solutions

TBD

### 2. Conclusion


---

## Developers

| Name       |  Number | Evaluation |
| ---------- | :-----: | :--------: |
| João Sousa | 1210631 |    100%    |
| João Brito | 1211711 |    100%    |
