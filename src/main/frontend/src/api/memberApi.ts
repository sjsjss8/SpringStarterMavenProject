import http from './http';

/**
 * 회원 도메인 API 호출 모듈 — 백엔드의 /api/v1/members/** 엔드포인트와 일대일 매핑.
 *
 * 컴포넌트는 이 모듈만 알고 axios/baseURL/엔드포인트 경로는 신경 쓰지 않는다.
 * (백엔드 라우팅이 바뀌어도 이 파일만 수정하면 됨)
 */

export interface Member {
  id: string;
  name: string;
  email: string;
}

export interface MemberCreateRequest {
  id: string;
  name: string;
  email: string;
}

export const memberApi = {
  /** GET /api/v1/members — 회원 전체 조회 */
  findAll(): Promise<Member[]> {
    return http.get<Member[]>('/members').then((res) => res.data as unknown as Member[]);
  },

  /** GET /api/v1/members/{id} — 단건 조회 */
  findById(id: string): Promise<Member> {
    return http.get<Member>(`/members/${id}`).then((res) => res.data as unknown as Member);
  },

  /** POST /api/v1/members — 회원 등록 */
  create(payload: MemberCreateRequest): Promise<Member> {
    return http.post<Member>('/members', payload).then((res) => res.data as unknown as Member);
  },

  /** DELETE /api/v1/members/{id} — 회원 삭제 */
  delete(id: string): Promise<Member> {
    return http.delete<Member>(`/members/${id}`).then((res) => res.data as unknown as Member);
  },
};
