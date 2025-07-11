
![logo](./images/logo.png)





## 💡 **팀원 소개**

### 👨🏻‍🎓 박지민

- 카이스트 전산학부 22학번

### 👩🏻‍🎓 이다인

- 한양대학교 컴퓨터소프트웨어학부 23학번

## 💡 프로젝트 소개

> 하루의 감정을 이모지, 텍스트, 사진으로 기록하고 월간 단위로 시각화해 확인할 수 있는 감정 기록 앱
> 

### ✅ 주요 기능

⭐ 로그인 / 회원가입 기능

⭐ 감정/상세 내용 입력 및 사진 추가 기능

⭐ 월간 감정 확인 기능

⭐ 맞춤형 알림 기능

### ✅ 기술 스택

- 개발 언어 : Kotlin
- 개발 환경 : Android Studio

---

### ✅ 스플래시 화면
![MindNote](images/splash.png)

### ✅ 회원가입 / 로그인 화면


- Firebase를 활용해 사용자 계정 관리
- 이메일 / 비밀번호 기반 회원 정보 관리
    1. 사용자 정보 입력 + 회원가입 완료 클릭
    2. Firebase Authentication에 계정 생성 요청
    3. Firestore의 users라는 폴더에 사용자의 UID를 파일 이름으로 저장해 추후 등장할 DayRecord(사용자가 입력하는 일일 기록)과 함께 저장

- Firebase에 저장된 정보와 입력한 이메일 / 비밀번호가 같은지 확인
- 로그인 성공 → 메인 화면으로 이동
- 로그인 실패 → “사용자 이름 혹은 비밀번호가 일치하지 않습니다”

### ✅ Tab 1 : Week page
![메인 화면](./images/week_page.png)

⭐ **앱의 메인 화면 (좌)**

<aside>

- 현재 주(월~일)의 감정과 기록을 한눈에 보고 관리할 수 있는 메인 탭
- 주요 기능
    - 주간 뷰: 현재 날짜가 속한 주의 월요일부터 일요일까지의 목록을 보여줌
    - 데일리 요약: 각 날짜 별로 선택된 이모지와 한 줄 요약을 표시
    - 상세 기록: + 버튼 또는 이모지를 클릭하면 상세 기록용 다이얼로그(Dialog)가 나타남
    - 데이터 자동 저장: Firebase에 Storage에 사용자 UID와 함께 자동으로 저장 및 동기화되어 앱을 재설치하고 다시 로그인해도 모든 기록 다시 확인 가능
    - 주간 초기화: 매주 월요일이 되면 기존 기록을 저장한 채 WeekRow가 초기화
</aside>

⭐ **상세 기록용 다이얼로그(Dialog) (우)**

<aside>

- + 버튼(기록 미입력) 또는 이모지(그날 입력한 이모지)를 클릭하면 상세 정보를 입력하는 DayDetailDialog 호출
- 한 줄 요약, 상세 설명, 오늘의 기분, 사진 첨부 기능
- 카메라 권한을 요청하고 그 상태를 hasCameraPermission에 저장
- ActivityResultContracts.TakePicture 런처를 사용해 카메라 실행 → 사진 바로 업로드
- ActivityResultContracts.GetContent 런처를 사용해 갤러리 실행 → 사진 선택 업로드
</aside>

### ✅ Tab 2 : Gallery Page

> WeekTab에서 사용자가 찍거나 선택한 사진들을 날짜 기준으로 정렬하여 격자 형태로 보여주는 페이지
> 
![갤러리 화면](./images/gallery_page.png)

### ✅ Tab 3 : Month page

> 사용자의 감정 변화를 한 달 단위로 시각적으로 확인할 수 있는 페이지
> 
![month_page](./images/month_page.png)

> 사용자가 Week Tab에서 기록한 정보가 자동 연동된다.
Kizitonwose CalendarView 라이브러리를 이용하여 달력 구현
> 


<aside>

⭐ **감정 이모지 달력 표시**

WeekTab에서 사용자가 선택한 감정 이모지를 달력 형태로 시각화하였다.

</aside>

<aside>

⭐ **날짜별 상세 기록 확인 기능**

달력에서 특정 날짜를 선택하면, 해당 날짜의 요약과 상세 내용을 확인할 수 있다.

</aside>



### ✅ Tab 4 : Profile Tab
![profile_page](./images/profile_page.png)


⭐ **내 프로필 (왼쪽 위)**

<aside>

- 로그인할 때 입력한 자신의 이메일 주소 확인 가능
- 닉네임을 설정해 Firebase에 같이 저장
</aside>


⭐ **알림 설정 (왼쪽 아래 + 오른쪽 그림)**

<aside>

- 퇴근 시간과 취침 시간을 설정해 사용자 맞춤형 알림 설정
</aside>


⭐ **푸쉬 알림**

<aside>

- 설정해놓은 퇴근 시간, 취침 시간에 푸쉬 알림
- ![Notification](images/notification.png)



  **다운로드 링크**

- https://github.com/JIMINDAIN/Android/releases/tag/v1.0.0
