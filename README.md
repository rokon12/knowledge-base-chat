# Knowledge Base Chat

A Java application that provides an interactive chat interface to query an industrial system knowledge base using AI. The application uses LangChain4j and OpenAI models to provide intelligent responses based on the knowledge base data.

## Features

- Interactive command-line chat interface
- RAG (Retrieval Augmented Generation) for accurate responses
- Knowledge base of industrial components and events
- Conversation memory to maintain context
- Comprehensive logging with Logback

## Requirements

- Java 21 or higher
- Maven 3.6 or higher
- OpenAI API key (or use "demo" for testing)

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/knowledge-base-chat.git
   cd knowledge-base-chat
   ```

2. Build the project:
   ```
   mvn clean package
   ```

3. Set your OpenAI API key (optional):
   ```
   export OPENAI_API_KEY=your_api_key_here
   ```

## Usage

Run the application:
```
java -jar target/knowledge-base-chat-1.0-SNAPSHOT.jar
```

Once the application starts, you can interact with the assistant:
```
Assistant: Hello! Ask me about the system components or known issues.
You: What components are in Sector A?
Assistant: [Response based on the knowledge base]
```

Type `exit` to end the session.

## Configuration

### Application Configuration

The application can be configured through the `src/main/resources/application.properties` file:

#### OpenAI API Configuration
```properties
# OpenAI API Configuration
# The API key is set via the OPENAI_API_KEY environment variable
openai.api.key=${OPENAI_API_KEY}
openai.chat.model=gpt-3.5-turbo
openai.embedding.model=text-embedding-ada-002
openai.log.requests=true
openai.log.responses=true
```

#### Content Retriever Configuration
```properties
# Content Retriever Configuration
retriever.max.results=3
retriever.min.score=0.6
```

#### Chat Memory Configuration
```properties
# Chat Memory Configuration
chat.memory.messages=10
```

#### Document Processing Configuration
```properties
# Document Processing Configuration
document.chunk.size=300
document.chunk.overlap=30
```

You can override these settings by modifying the properties file. The OpenAI API key can also be set using the `OPENAI_API_KEY` environment variable, which takes precedence over the property file setting.

### Logging

Logging is configured in `src/main/resources/logback.xml`. The application logs to both console and file:
- Console: Simple format with timestamp, thread, level, logger, and message
- File: Detailed format with date, time, thread, level, logger, and message
  - Log files are stored in the `logs` directory
  - Files are rolled based on size (10MB) and date
  - Maximum history of 30 days and total size cap of 1GB

### Knowledge Base

The knowledge base consists of two files:
- `components.txt`: Contains information about system components
- `knowledge.txt`: Contains events, rules, maintenance notes, and safety procedures

## Project Structure

```
knowledge-base-chat/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ca/
│   │   │       └── bazlur/
│   │   │           ├── KnowledgeAssistant.java
│   │   │           ├── config/
│   │   │           │   ├── AppConfig.java
│   │   │           │   └── ConfigProvider.java
│   │   │           └── service/
│   │   │               ├── AssistantService.java
│   │   │               └── KnowledgeBaseService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── components.txt
│   │       ├── knowledge.txt
│   │       └── logback.xml
│   └── test/
│       └── java/
│           └── ca/
│               └── bazlur/
│                   ├── config/
│                   │   └── AppConfigTest.java
│                   └── service/
│                       ├── AssistantServiceTest.java
│                       └── KnowledgeBaseServiceTest.java
├── pom.xml
└── README.md
```

- `KnowledgeAssistant.java`: Main class that handles the chat interface
- `config/AppConfig.java`: Provides application configuration from properties file
- `config/ConfigProvider.java`: Interface for configuration values to enable dependency injection
- `service/AssistantService.java`: Service that handles the assistant functionality
- `service/KnowledgeBaseService.java`: Service that loads and processes the knowledge base data
- `application.properties`: Configuration file for the application
- `components.txt`: Contains information about system components
- `knowledge.txt`: Contains events, rules, maintenance notes, and safety procedures
- `logback.xml`: Logging configuration

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
