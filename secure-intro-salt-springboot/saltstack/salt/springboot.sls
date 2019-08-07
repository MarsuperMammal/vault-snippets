spring-si-boot:
  service.running:
    - enable: True
  file.managed:
    - name: /etc/secret-id
    - source: salt://springboot/files/secret-id
    - user: root
    - group: root
    - mode: 644
    - template: jinja
