# CMMS11 운영 서버 환경 점검 사항

## 1. 서버 환경 점검

### 시스템 요구사항
- **OS**: Ubuntu 20.04 LTS 이상
- **Java**: OpenJDK 17 이상
- **메모리**: 최소 4GB RAM (권장 8GB)
- **디스크**: 최소 20GB 여유 공간
- **CPU**: 최소 2코어

### 필수 소프트웨어 설치
```bash
# Java 설치 확인
java -version
javac -version

# MariaDB 설치 및 설정
sudo apt update
sudo apt install mariadb-server mariadb-client

# 방화벽 설정
sudo ufw allow 8080/tcp
sudo ufw allow 3306/tcp
sudo ufw enable
```

## 2. 데이터베이스 설정

### MariaDB 설정
```sql
-- 프로덕션 데이터베이스 생성
CREATE DATABASE cmms11 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 프로덕션 사용자 생성
CREATE USER 'cmms11_prod'@'localhost' IDENTIFIED BY '강력한_비밀번호';
GRANT ALL PRIVILEGES ON cmms11.* TO 'cmms11_prod'@'localhost';
FLUSH PRIVILEGES;

-- 보안 설정
DELETE FROM mysql.user WHERE User='';
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
DROP DATABASE IF EXISTS test;
DELETE FROM mysql.db WHERE Db='test' OR Db='test\\_%';
FLUSH PRIVILEGES;
```

### 데이터베이스 백업 설정
```bash
# 백업 스크립트 생성
sudo mkdir -p /opt/backups/cmms11
sudo chown $USER:$USER /opt/backups/cmms11

# crontab에 백업 작업 추가
crontab -e
# 매일 새벽 2시에 백업
0 2 * * * mysqldump -u cmms11_prod -p'비밀번호' cmms11 > /opt/backups/cmms11/cmms11_$(date +\%Y\%m\%d).sql
```

## 3. 애플리케이션 배포

### 디렉토리 구조 설정
```bash
sudo mkdir -p /opt/cmms11/{storage/uploads,logs,backups}
sudo chown -R $USER:$USER /opt/cmms11
```

### 환경 변수 설정
```bash
# /etc/environment에 추가
sudo nano /etc/environment
# 추가할 내용:
DB_PASSWORD=강력한_데이터베이스_비밀번호
AWS_S3_BUCKET=prodYULSLAB-bucket
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

## 4. 보안 설정

### SSL/TLS 인증서 설정
```bash
# Let's Encrypt 인증서 설치 (Nginx 사용 시)
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

### 방화벽 설정
```bash
# 필요한 포트만 열기
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw enable
```

## 5. 모니터링 설정

### 로그 로테이션 설정
```bash
sudo nano /etc/logrotate.d/cmms11
# 내용:
/opt/cmms11/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 $USER $USER
    postrotate
        systemctl reload cmms11
    endscript
}
```

### 시스템 모니터링
```bash
# htop 설치
sudo apt install htop

# 디스크 사용량 모니터링
df -h
du -sh /opt/cmms11/*

# 메모리 사용량 확인
free -h
```

## 6. 성능 최적화

### JVM 튜닝
```bash
# application-prod.yml에서 JAVA_OPTS 조정
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### MariaDB 최적화
```bash
sudo nano /etc/mysql/mariadb.conf.d/50-server.cnf
# 주요 설정:
[mysqld]
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
max_connections = 200
query_cache_size = 64M
```

## 7. 백업 및 복구

### 자동 백업 스크립트
```bash
#!/bin/bash
# /opt/scripts/backup-cmms11.sh

BACKUP_DIR="/opt/backups/cmms11"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="cmms11"
DB_USER="cmms11_prod"
DB_PASS="비밀번호"

# 데이터베이스 백업
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/db_$DATE.sql

# 파일 백업
tar -czf $BACKUP_DIR/files_$DATE.tar.gz /opt/cmms11/storage/uploads

# 오래된 백업 삭제 (30일 이상)
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
```

## 8. 배포 체크리스트

### 배포 전 확인사항
- [ ] 데이터베이스 연결 테스트
- [ ] 파일 업로드 디렉토리 권한 확인
- [ ] 로그 디렉토리 권한 확인
- [ ] 환경 변수 설정 확인
- [ ] 방화벽 설정 확인
- [ ] SSL 인증서 유효성 확인

### 배포 후 확인사항
- [ ] 애플리케이션 정상 시작 확인
- [ ] 웹 페이지 접근 테스트
- [ ] 데이터베이스 연결 테스트
- [ ] 파일 업로드/다운로드 테스트
- [ ] 로그 파일 생성 확인
- [ ] 백업 스크립트 실행 테스트

## 9. 장애 대응

### 일반적인 문제 해결
```bash
# 서비스 상태 확인
sudo systemctl status cmms11

# 로그 확인
sudo journalctl -u cmms11 -f

# 포트 사용 확인
sudo netstat -tlnp | grep :8080

# 프로세스 확인
ps aux | grep java
```

### 긴급 복구 절차
1. 서비스 중지: `sudo systemctl stop cmms11`
2. 백업에서 복구: `mysql -u cmms11_prod -p cmms11 < backup.sql`
3. 서비스 재시작: `sudo systemctl start cmms11`
4. 상태 확인: `sudo systemctl status cmms11`
