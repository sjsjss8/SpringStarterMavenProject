// DOM 요소 참조
const memberListElement = document.querySelector('#memberList');
const modal = document.querySelector('#memberModal');
const modalTitle = document.querySelector('#modalTitle');
const memberId = document.querySelector('#memberId');
const memberName = document.querySelector('#memberName');
const memberEmail = document.querySelector('#memberEmail');
const saveBtn = document.querySelector('#saveBtn');
const deleteBtn = document.querySelector('#deleteBtn');
const cancelBtn = document.querySelector('#cancelBtn');
const addMemberBtn = document.querySelector('#addMemberBtn');
const searchType = document.querySelector('#searchType');
const searchKeyword = document.querySelector('#searchKeyword');
const searchBtn = document.querySelector('#searchBtn');

// 모달 관련 함수
const modalHandler = {
    open: (isEdit = false) => {
        modal.style.display = 'block';
        modalTitle.textContent = isEdit ? '회원 수정' : '회원 추가';
        deleteBtn.style.display = isEdit ? 'block' : 'none';
        clearInputs();
    },
    close: () => {
        modal.style.display = 'none';
        clearInputs();
    }
};

// 유틸리티 함수
const clearInputs = () => {
    memberId.value = '';
    memberName.value = '';
    memberEmail.value = '';
};

const validateInputs = () => {
    if (!memberName.value.trim() || !memberEmail.value.trim()) {
        alert('입력하지 않은 정보가 있습니다');
        return false;
    }
    return true;
};

// 이벤트 핸들러
const handleAdd = () => {
	modalHandler.open(false);
}

const handleSave = async () => {
    if (!validateInputs()) return;

    const memberData = {
        name: memberName.value.trim(),
        email: memberEmail.value.trim()
    };

    try {
        if (memberId.value) {
            await RESTful.api.update(memberId.value, memberData);
        } else {
            await RESTful.api.insert(memberData);
        }
        alert('저장되었습니다');
        modalHandler.close();
        loadMembers();
    } catch (error) {
        alert('저장에 실패했습니다');
        console.error(error);
    }
};

const handleDelete = async () => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await RESTful.api.delete(memberId.value);
        alert('삭제되었습니다');
        modalHandler.close();
        loadMembers();
    } catch (error) {
        alert('삭제에 실패했습니다');
        console.error(error);
    }
};

// 테이블 렌더링 함수
const renderTable = (members) => {
    const tbody = document.querySelector('.member-table tbody');
    
    if (!members?.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="empty-message">등록된 회원이 없습니다</td></tr>';
        return;
    }

    tbody.innerHTML = members.map(member => `
        <tr>
            <td class="member-id" data-id="${member.id}">${member.id}</td>
            <td>${member.name}</td>
            <td>${member.email}</td>
        </tr>
    `).join('');

    // ID 클릭 이벤트 추가
    document.querySelectorAll('.member-id').forEach(td => {
        td.style.cursor = 'pointer';
        td.addEventListener('click', () => handleEdit(td.dataset.id));
    });
};

// 회원 정보 수정 처리
const handleEdit = async (id) => {
    try {
        const member = await RESTful.api.findById(id);
        
        memberId.value = member.data.id;
        memberName.value = member.data.name;
        memberEmail.value = member.data.email;
        modalHandler.open(true);
    } catch (error) {
        alert('회원 정보 조회에 실패했습니다');
        console.error(error);
    }
};

// 회원 목록 로드
const loadMembers = async () => {
    try {
        const response = await RESTful.api.findAll();
        renderTable(response.data);
    } catch (error) {
        alert('회원 목록 조회에 실패했습니다');
        console.error(error);
    }
};

// 검색 처리 함수
const handleSearch = async () => {
    try {
        const response = await RESTful.api.search({
            type: searchType.value,
            keyword: searchKeyword.value.trim()
        });
        renderTable(response.data);
    } catch (error) {
        alert('검색에 실패했습니다');
        console.error(error);
    }
};

// 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', () => {
    addMemberBtn.addEventListener('click', handleAdd);
    saveBtn.addEventListener('click', handleSave);
    deleteBtn.addEventListener('click', handleDelete);
    cancelBtn.addEventListener('click', modalHandler.close);
    searchBtn.addEventListener('click', handleSearch);
    
    // 엔터키 검색 지원
    searchKeyword.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleSearch();
    });

    // 초기 데이터 로드
    loadMembers();
});