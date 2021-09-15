import { Component, OnInit } from '@angular/core';
import {ApiService} from "../../services/api.service";

@Component({
  selector: 'app-record',
  templateUrl: './record.component.html',
  styleUrls: ['./record.component.scss']
})
export class RecordComponent implements OnInit {

  debug = false;
  showAllProperties = false;
  searchFilter = '';
  searchBase = '';

  filterOptions = ['', 'cn=', 'sAMAccountName=']
  filterOption = this.filterOptions[1];

  defaultProperties = ['cn', 'dn', 'objectClass', 'manager', 'managedBy', 'sAMAccountName'];
  allProperties = [];

  record$: any;
  searchResult$: any;

  dnKey = 'dn';

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.config();
  }

  public config() {
    this.api.config().subscribe(
      (next: any) => {
        this.dnKey = next.dnKey;
      },
      error => {
        console.error(error);
      }
    )
  }

  public getPropertyOrNull(property: string, ldapRecord: any) {
    if(ldapRecord[property]) {
      console.log(ldapRecord)
      return ldapRecord[property];
    }
    return null;
  }

  public search() {
    // this.record$ = this.api.getUser(this.searchInput);
    this.searchResult$ = this.api.search(this.searchBase, this.filterOption + this.searchFilter);
  }

  /**
   * Should return true if this is a DN, this is a complex subject.
   * @param value
   */
  public isSearchExactable(value: string) {
    return value && value.includes('=');
  }

  public searchExact(entryDN: string) {
    this.record$ = this.api.getUser(entryDN);
    this.record$.subscribe(
      next => {
        this.allProperties = Object.keys(next['record'][0]);
      },
      error => {
        console.error(error);
      }
    )
  }

}
