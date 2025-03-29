import {Component, OnDestroy, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {marked} from 'marked';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'frontend';

  private socket!: WebSocket;
  message: string = '';
  currentResponse: string = '';
  messages: string[] = [];
  choices: string[] = [];

  constructor(private httpClient: HttpClient) {
  }

  ngOnInit(): void {
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.socket.close();
  }

  private connectWebSocket() {
    this.socket = new WebSocket('ws://localhost:8080/ws/chat');

    this.socket.onopen = () => {
      console.log('WebSocket connected!');
    };

    this.socket.onmessage = (event) => {
      const wholeMessage = event.data;

      const match: string = wholeMessage.match(/<choices>(.*?)<\/choices>/);

      if (match && match[1]) {
        try {
          this.choices = JSON.parse(match[1]);
        } catch (error) {
          console.error("Kunde inte parsea JSON choices:", error);
          this.choices = [];
        }
      }

      if (wholeMessage.includes('<complete-response>')) {
        const message = event.data.substring(19, event.data.length - 20);

        console.log('complete response')
        console.log(message)

        this.messages.push(message);
        this.currentResponse = '';
      } else {
        this.currentResponse += event.data;
      }
    };

    this.socket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    this.socket.onclose = () => {
      console.log('WebSocket closed!');
    };
  }

  send() {
    if (this.message.trim()) {
      this.socket.send(this.message);
      this.message = '';
      this.choices = [];
    }
  }

  getUsers() {
    this.httpClient.get<any>('http://localhost:8080/api/users').subscribe(users => {
      console.log(users);
    })
  }

  addUser() {
    this.httpClient.post('http://localhost:8080/api/users', {name: 'Mattias', email: 'mattias@example.com'})
      .subscribe(user => {
        console.log(user);
      })
  }

  getMarkdownMessage(msg: string) {
    return marked(msg).toString();
  }

  makeChoice(choice: string) {
    this.socket.send('choice:' + choice);
    this.choices = [];
  }

  test1() {
    console.log(this.messages)
  }

  test2() {
    console.log(this.currentResponse)
  }
}
