{{/*
Return the proper Docker Image Registry Secret Names
*/}}
{{- define "omar-geoscript.imagePullSecrets" -}}
  {{/*
    Build the secrets list out of .Values.image.pullSecrets, .Values.global.image.pullSecrets,
    and the generated secret (if it exists).
  */}}
  {{- $secrets := list -}}
  {{- if .Values.image -}}
    {{- if .Values.image.pullSecrets -}}
      {{- $secrets = (concat $secrets .Values.image.pullSecrets | uniq) }}
    {{- end -}}
  {{- end -}}
  {{- if .Values.global -}}
    {{- if .Values.global.image.pullSecrets -}}
      {{- $secrets = (concat $secrets .Values.global.image.pullSecrets | uniq) }}
    {{- end -}}
  {{- end -}}
  {{- if .Values.generatedImagePullSecret -}}
    {{- if .Values.generatedImagePullSecret.create -}}
      {{- $secrets = (append $secrets (.Values.generatedImagePullSecret.name | default (include "omar-geoscript.fullname" .))) }}
    {{- end -}}
  {{- end -}}

  {{- if not (empty $secrets) }}
imagePullSecrets:
      {{- range $secrets }}
  - name: {{ tpl . $ }}
      {{- end -}}
  {{- end }}
{{- end -}}


{{- define "omar-geoscript.generateImagePullSecret" }}
{{- printf "{\"auths\": {\"%s\": {\"auth\": \"%s\"}}}" (.Values.generatedImagePullSecret.registry) (printf "%s:%s" .Values.generatedImagePullSecret.username .Values.generatedImagePullSecret.password | b64enc) | b64enc }}
{{- end }}