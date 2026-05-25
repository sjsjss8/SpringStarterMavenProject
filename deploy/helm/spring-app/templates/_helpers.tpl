{{/*
  공통 헬퍼 — 차트 안의 모든 템플릿에서 재사용.
*/}}

{{/* 리소스 이름 (release-name 기반, fullnameOverride 우선) */}}
{{- define "spring-app.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/* 차트 짧은 이름 */}}
{{- define "spring-app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/* 표준 라벨 — 모든 리소스에 일관되게 부착 */}}
{{- define "spring-app.labels" -}}
app.kubernetes.io/name: {{ include "spring-app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end -}}

{{/* 셀렉터 라벨 — Deployment 와 Service 가 매칭에 사용 */}}
{{- define "spring-app.selectorLabels" -}}
app.kubernetes.io/name: {{ include "spring-app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
