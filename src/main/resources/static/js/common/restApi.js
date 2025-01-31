const RESTful = {
    // Axios 인스턴스 생성 및 기본 설정
    client: axios.create({
        baseURL: '/api/v1', // API 버전 명시
        timeout: 5000,      // 타임아웃 설정
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    }),

    // RESTful API 서비스
    api: {
        // Create
        insert(resource, data) {
            return this.client.post(`/${resource}`, data)
                .then(response => response.data)
                .catch(this.handleError);
        },

        // Read
        findAll(resource, params) {
            return this.client.get(`/${resource}`, { params })
                .then(response => response.data)
                .catch(this.handleError);
        },

        search(resource, params) {
            return this.client.get(`/${resource}/search`, { params })
                .then(response => response.data)
                .catch(this.handleError);
        },

        findById(resource, id) {
            return this.client.get(`/${resource}/${id}`)
                .then(response => response.data)
                .catch(this.handleError);
        },

        // Update
        update(resource, id, data) {
            return this.client.put(`/${resource}/${id}`, data)
                .then(response => response.data)
                .catch(this.handleError);
        },

        // Delete
        delete(resource, id) {
            return this.client.delete(`/${resource}/${id}`)
                .then(response => response.data)
                .catch(this.handleError);
        },
        
        // Bulk 등등
    },

    // 공통 에러 처리
    handleError(error) {
        if (error.response) {
            const { status, data } = error.response;
            switch (status) {
                case 400: throw new Error(`잘못된 요청입니다: ${data.message}`);
                case 401: throw new Error('인증이 필요합니다.');
                case 403: throw new Error('접근 권한이 없습니다.');
                case 404: throw new Error('요청한 리소스를 찾을 수 없습니다.');
                case 409: throw new Error('리소스 충돌이 발생했습니다.');
                case 500: throw new Error('서버 오류가 발생했습니다.');
                default: throw new Error('알 수 없는 오류가 발생했습니다.');
            }
        }
        throw error;
    },

    // 인터셉터 설정
    setupInterceptors() {
        // 요청 인터셉터
        this.client.interceptors.request.use(
            config => {
                // 요청 전 처리
                const token = localStorage.getItem('token');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            error => Promise.reject(error)
        );

        // 응답 인터셉터
        this.client.interceptors.response.use(
            response => response,
            error => {
                // 응답 에러 처리
                if (error.response?.status === 401) {
                    localStorage.removeItem('token');
                    window.location.href = '/login';
                }
                return Promise.reject(error);
            }
        );
    }
};

// 인터셉터 초기화
RESTful.setupInterceptors();