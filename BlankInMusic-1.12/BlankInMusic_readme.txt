BlankInMusic 사용설명서
	명령어
		/blankinmusic : 플러그인의 정상작동을 확인함. 플러그인의 변수를 초기화함.

		/musictag <태그> : 손에 들고 있는 아이템을 악기로 만듭니다.
			└태그 목록 : 기타, 종, 차임벨, 드럼, 하프, 플루트, 플링, 실로폰, 베이스
			└태그에 띄어쓰기 적용 가능
		
		/tagadd <소리경로> <새로운 태그>
			└/tagadd block.note.harp 피아노 : 입력시 피아노 태그에 harp 소리 할당
			└새로운 태그로 drum.3 ~ drum.14 등록시 드럼 소리 추가
			└태그에 띄어쓰기 적용 가능
		
		/연주차단 : 악기소리를 차단/차단해제 합니다.

		/악보등록 : 손에 들고 있는 악보를 등록합니다.
		
		/악보연주 : 등록된 악보를 손에 들고있는 악기로 연주합니다.
		
		/악보연결 : 등록된 악보에 이어서 등록합니다.
		
		/지휘자 <닉네임> : 해당 유저를 지휘자로 등록합니다. 지휘자가 악보연주를 시작하면 동시에 연주를 시작합니다.

	권한
		관리자 BlankInMusic.op.*
			BlankInMusic.blankinmusic
			BlankInMusic.op.musictag
			BlankInMusic.op.tagadd

		유저  BlankInMusic.user.*
			BlankInMusic.user.연주차단
			BlankInMusic.user.악보등록
			BlankInMusic.user.악보연주
			BlankInMusic.user.지휘자
			
		BlankInMusic.user.악보연결
		└악용 가능성이 있어 권한을 나눠놓았음