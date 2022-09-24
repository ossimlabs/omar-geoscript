
{{/*
Templates for the volumeMounts section
*/}}

{{- define "omar-geoscript.volumeMounts.configmaps" -}}
{{- range $configmapName, $configmapDict := .Values.configmaps}}
- name: {{ $configmapName | quote }}
  mountPath: {{ $configmapDict.mountPath | quote }}
  {{- if $configmapDict.subPath }}
  subPath: {{ $configmapDict.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}

{{- define "omar-geoscript.volumeMounts.secrets" -}}
{{- range $secretName, $secretDict := .Values.secrets}}
- name: {{ $secretName | quote }}
  mountPath: {{ $secretDict.mountPath | quote }}
  {{- if $secretDict.subPath }}
  subPath: {{ $secretDict.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}

{{- define "omar-geoscript.volumeMounts.pvcs" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- name: {{ $volumeName }}
  mountPath: {{ $volumeDict.mountPath }}
  {{- if $volumeDict.subPath }}
  subPath: {{ $volumeDict.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}

{{- define "omar-geoscript.volumeMounts" -}}
{{- include "omar-geoscript.volumeMounts.configmaps" . -}}
{{- include "omar-geoscript.volumeMounts.secrets" . -}}
{{- include "omar-geoscript.volumeMounts.pvcs" . -}}
{{- if .Values.global.extraVolumeMounts }}
{{ toYaml .Values.global.extraVolumeMounts }}
{{- end }}
{{- if .Values.extraVolumeMounts }}
{{ toYaml .Values.extraVolumeMounts }}
{{- end }}
{{- end -}}




{{/*
Templates for the volumes section
 */}}

{{- define "omar-geoscript.volumes.configmaps" -}}
{{- range $configmapName, $configmapDict := .Values.configmaps}}
- name: {{ $configmapName | quote }}
  configMap:
    name: {{ $configmapName | quote }}
{{- end -}}
{{- end -}}

{{- define "omar-geoscript.volumes.secrets" -}}
{{- range $secretName, $secretDict := .Values.secrets}}
- name: {{ $secretName | quote }}
  secret:
    secretName: {{ $secretName | quote }}
{{- end -}}
{{- end -}}

{{- define "omar-geoscript.volumes.pvcs" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- name: {{ $volumeName }}
  persistentVolumeClaim:
    claimName: "{{ include "omar-geoscript.fullname" $ }}-{{ $volumeName }}-pvc"
{{- end -}}
{{- end -}}

{{- define "omar-geoscript.volumes" -}}
{{- include "omar-geoscript.volumes.configmaps" . -}}
{{- include "omar-geoscript.volumes.secrets" . -}}
{{- include "omar-geoscript.volumes.pvcs" . -}}
{{- if .Values.global.extraVolumes }}
{{ toYaml .Values.global.extraVolumes }}
{{- end }}
{{- if .Values.extraVolumes }}
{{ toYaml .Values.extraVolumes }}
{{- end }}
{{- end -}}
