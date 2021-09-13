import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient) { }

  public config() {
    return this.http.get('/api/v1/ldap/config');
  }

  public getUser(userDn: string) {
    return this.http.get(`/api/v1/ldap/by-dn/${userDn}`);
  }

  public search(base: string, filter: string) {
    var params = new HttpParams();
    params = params.set('filter', filter);
    if(base.length === 0) {
      base = null;
    }

    return this.http.get(`/api/v1/ldap/${base}?${params.toString()}`);
  }
}
