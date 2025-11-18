// 멀티파트(FormData) 조립 유틸
// 서버의 @RequestPart dto(JSON) + 파일 규약을 맞추기 쉽도록 도와줍니다.
export function formWithJson(dtoPartName, dtoObject) {
    const fd = new FormData();
    const blob = new Blob([JSON.stringify(dtoObject)], { type: "application/json" });
    fd.append(dtoPartName, blob);
    return fd;
}

export function appendFile(fd, fieldName, file) {
    if (file) fd.append(fieldName, file);
    return fd;
}
