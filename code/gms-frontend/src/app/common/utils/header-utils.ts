import { HttpHeaders } from "@angular/common/http";


export function getHeaders() : HttpHeaders {
    const headers = new HttpHeaders({timeout: '15000'})
    .set('Content-Type', 'application/json; charset=utf-8');
    return headers;
}
