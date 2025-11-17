# WEEK9_FE
# 프로젝트 설정 안내
- 현재 프로젝트의 시작 화면은 "http://localhost:8080/html/login.html" 를 주소 창에 입력해주세요.
- 로컬 환경에서 실행하기 전에 아래 두 가지 설정을 반드시 사용자 환경에 맞게 수정해 주세요.

## 1) 데이터베이스(DB) 설정
application.yml에서는 데이터베이스 연결 정보를 환경 변수로 읽도록 구성되어 있습니다:
- DB_URL (예: jdbc:mysql://localhost:3306/your_db_name?serverTimezone=UTC&useSSL=false)
- DB_PASSWORD

설정 방법 예시:
- macOS / Linux (bash/zsh)
    - export DB_URL='jdbc:mysql://localhost:3306/mydb?serverTimezone=UTC&useSSL=false'
    - export DB_PASSWORD='your_password'
- Windows (PowerShell)
    - $env:DB_URL = 'jdbc:mysql://localhost:3306/mydb?serverTimezone=UTC&useSSL=false'
    - $env:DB_PASSWORD = 'your_password'

또는 IDE 실행 구성(Run Configuration)에서 환경 변수로 설정해주셔도 됩니다.

참고:
- application.yml의 jpa.hibernate.ddl-auto는 현재 `validate`로 되어 있습니다. 개발 중에는 필요에 따라 `create`, `update` 등으로 임시 변경할 수 있습니다.
- 드라이버는 com.mysql.cj.jdbc.Driver 를 사용합니다.

## 2) 프로필/게시글 사진 저장 디렉토리
application.yml의 file.profileDir 및 file.postDir 항목을 현재 로컬 경로에 맞게 변경해야 합니다.  
현재 예시 경로는 절대 경로 형태로 되어 있으므로 OS와 프로젝트 위치에 맞춰 수정하세요.

권장 방식:
- 개발 환경에서는 프로젝트 내부의 상대 경로 사용(예: src/main/resources/static/assets/images/...) — 단, 빌드/배포 시 정적 리소스 처리에 유의
- 운영 환경에서는 애플리케이션 외부의 절대 경로 사용(예: /var/myapp/uploads/...) — 파일 접근 권한과 백업 고려

예시:
- macOS / Linux (절대 경로)
    - file:
      profileDir: /home/username/myapp/uploads/profile/
      postDir: /home/username/myapp/uploads/post/
- Windows (절대 경로, 백슬래시 이스케이프 주의)
    - file:
      profileDir: C:/projects/myapp/uploads/profile/
      postDir: C:/projects/myapp/uploads/post/
- 상대 경로 예시 (프로젝트 루트 기준)
    - file:
      profileDir: ./uploads/profile/
      postDir: ./uploads/post/

추가 권장사항:
- 디렉토리가 존재하지 않으면 애플리케이션 시작 시 생성하도록 코드에서 처리하거나, 수동으로 미리 생성하세요.

## 3) 기타 설정 확인
- 업로드 최대 파일 크기: application.yml에 `spring.servlet.multipart.max-file-size: 10MB`로 설정되어 있습니다. 필요 시 변경하세요.
- 로깅: Hibernate SQL 로그 레벨이 off로 설정되어 있습니다. 디버깅 시 레벨을 올릴 수 있습니다.